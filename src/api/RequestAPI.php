<?php declare(strict_types=1);
/*
 * PoiXson WebLinkMC - Link your website with your mc server
 * @copyright 2023
 * @license AGPL-3
 * @author lorenzo at poixson.com
 * @link https://poixson.com/
 */
namespace pxn\WebLinkMC\api;

use \pxn\phpUtils\tools\JsonChunkProcessor;


interface RequestAPI extends JsonChunkProcessor {


	public function getRequestJSON(): string;

	public function isDone(): bool;


}
